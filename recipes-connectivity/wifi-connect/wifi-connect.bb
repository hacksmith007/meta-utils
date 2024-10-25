DESCRIPTION = "Recipe to Connect to wifi on startup"
HOMEPAGE = "https://yoctoproject.org"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "file://wpa_supplicant.conf \
           file://wifi-connect.service"

DEPENDS += "wpa-supplicant"           

# Install files
do_install() {
    # Install the wpa_supplicant configuration
    install -d ${D}${sysconfdir}/wpa_supplicant
    install -m 600 ${WORKDIR}/wpa_supplicant.conf ${D}${sysconfdir}/wpa_supplicant/wpa_supplicant.conf

    # Install the systemd service file
    install -d ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/wifi-connect.service ${D}${systemd_system_unitdir}/wifi-connect.service
}

# Ensure the service is enabled at boot
SYSTEMD_SERVICE:${PN} = "wifi-connect.service"
SYSTEMD_AUTO_ENABLE = "enable"

FILES:${PN} += "${systemd_system_unitdir}/wifi-connect.service"
