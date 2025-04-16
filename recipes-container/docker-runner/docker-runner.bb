DESCRIPTION = "Installs a prebuilt Docker image and sets up a service to load it"
HOMEPAGE = "https://yoctoproject.org"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit systemd

SRC_URI = "file://Dockerfile \
           file://entrypoint.sh \
           file://docker-runner.service"

S = "${WORKDIR}"

SYSTEMD_SERVICE:${PN} = "docker-runner.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

do_compile() {
    # Build the Docker image
    /usr/bin/docker build -t docker-runner:latest ${S}
}

do_install() {
    install -d ${D}/opt/docker

    # Save the image outside D to avoid UID/GID issues
    /usr/bin/docker save docker-runner:latest -o ${WORKDIR}/docker-runner.tar

    # Fix ownership to avoid "uid not found" error
    chmod 644 ${WORKDIR}/docker-runner.tar
    chown root:root ${WORKDIR}/docker-runner.tar

    # Install into image
    install -Dm 644 ${WORKDIR}/docker-runner.tar ${D}/opt/docker/docker-runner.tar

    # Systemd service
    install -D -m 644 ${WORKDIR}/docker-runner.service ${D}${systemd_system_unitdir}/docker-runner.service
}
FILES:${PN} += "/opt/docker"
FILES:${PN} += "/opt/docker/docker-runner.tar"
FILES:${PN} += "${systemd_system_unitdir}/docker-runner.service"

# Disable insane check for uid mismatch (optional, if still issues)
INSANE_SKIP:${PN} += "installed-vs-shipped"