DESCRIPTION = "Recipe to install Netconf Server and yangCliPro"
HOMEPAGE = "https://yoctoproject.org"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit autotools pkgconfig systemd

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRCREV_yuma = "30360b8fbccc089ea6efe10e5148577c23d5652c"

BRANCH_yuma = "master"
BRANCH_fwd = "devR1.0"
BRANCH_yang = "devR1.0"

# Base paths
S_yuma = "${WORKDIR}/yuma"
S_yang = "${WORKDIR}/yang"
S_forwardplane = "${WORKDIR}/forwardplane"

python __anonymous() {
    import os
    import bb
    import subprocess

    def get_git_commit(path):
        try:
            return subprocess.check_output(["git", "-C", path, "rev-parse", "HEAD"]).decode("utf-8").strip()
        except Exception as e:
            bb.warn(f"⚠️ Failed to get git commit from {path}: {e}")
            return ""

    src_list = []

    # yuma fixed
    src_list.append("git://github.com/hacksmith007/openyuma.git;protocol=https;branch=alpha_devR1.0;name=yuma;rev=30360b8fbccc089ea6efe10e5148577c23d5652c;destsuffix=yuma")

    local_fwd = "/workspace/yocto-sources/aether/forwardplane"
    if os.path.exists(local_fwd):
        rev_fwd = get_git_commit(local_fwd)
        src_list.append(f"git://{local_fwd};protocol=file;branch=devR1.0;name=forwardplane;rev={rev_fwd};destsuffix=forwardplane")
    else:
        src_list.append("git://github.com/hacksmith007/forwardplane.git;protocol=https;branch=devR1.0;name=forwardplane;rev=<replace_with_sha>;destsuffix=forwardplane")

    local_yang = "/workspace/yocto-sources/aether/yang"
    if os.path.exists(local_yang):
        rev_yang = get_git_commit(local_yang)
        src_list.append(f"git://{local_yang};protocol=file;branch=devR1.0;name=yang;rev={rev_yang};destsuffix=yang")
    else:
        src_list.append("git://github.com/hacksmith007/yang.git;protocol=https;branch=devR1.0;name=yang;rev=<replace_with_sha>;destsuffix=yang")

    src_list += [
        "file://netconfd.service",
        "file://netconfd.conf",
        "file://startup-cfg.xml"
    ]

    d.setVar("SRC_URI", " ".join(src_list))
}

DEPENDS += "git autoconf automake pkgconfig gcc libtool libxml2 libssh2 zlib readline openssl openssh ncurses zlib"

do_configure:prepend() {
    # Copy modules
    mkdir -p ${S_yuma}/example-modules/aether-networks
    cp -r ${S_yang}/* ${S_yuma}/example-modules/aether-networks || true
    cp -r ${S_forwardplane}/yuma/aether-xcvr/* ${S_yuma}/example-modules/aether-networks|| true
}

do_configure() {
    cd ${S_yuma}
    autoreconf -i -f
    ./configure CFLAGS='-g -O0' CXXFLAGS='-g -O0' --prefix=${prefix} --host=${TARGET_SYS}
}

do_compile() {
    cd ${S_yuma}
    oe_runmake
}

do_install() {
    cd ${S_yuma}
    install -d ${D}${libdir}/yuma
    install -Dm 0644 ${WORKDIR}/startup-cfg.xml ${D}/etc/config/startup-cfg.xml
    install -Dm 0644 ${WORKDIR}/netconfd.conf ${D}/etc/yuma/netconfd.conf
    oe_runmake DESTDIR=${D} install

    install -Dm 0644 ${WORKDIR}/netconfd.service ${D}${systemd_system_unitdir}/netconfd.service
}

SYSTEMD_SERVICE:${PN} = "netconfd.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

FILES:${PN} += "${systemd_system_unitdir}/netconfd.service"
FILES:${PN} += "/etc/config/startup-cfg.xml"
FILES:${PN} += "/etc/yuma/netconfd.conf"
FILES:${PN} += "${libdir}/yuma/*"
FILES:${PN} += "/usr/share/yuma/*"

INSANE_SKIP:${PN} += "la"
INSANE_SKIP:${PN} += "dev-so"