DESCRIPTION = "Recipe to install Netconf Server and yangCliPro"
HOMEPAGE = "https://yoctoproject.org"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI_yuma ?= "github.com/hacksmith007/openyuma"
SRC_URI_aether ?= "github.com/hacksmith007/AetherLink-yang"
SRC_URI_yuma_local ?= "/home/rahul/yocto-project/rasp/openyuma"
SRC_URI_aether_local ?= "/home/rahul/yocto-project/rasp/AetherLink-yang"

PROTOCOL_aether ?= "file"
PROTOCOL_yuma ?= "file"

BRANCH_aether = "main"
BRANCH_yuma = "sil_init_aether_xcvr"

SRC_URI = "git://${SRC_URI_yuma_local};protocol=${PROTOCOL_yuma};branch=${BRANCH_yuma};name=yuma;destsuffix=yuma \
           git://${SRC_URI_aether_local};protocol=${PROTOCOL_aether};branch=${BRANCH_aether};name=ather;destsuffix=aether"

SRC_URI += "file://netconfd.service \
            file://netconfd.conf \
            file://startup-cfg.xml "


SRCREV_yuma = "bd693b71e685cdc8db24f96dbf61ef7d1d0ba723"
SRCREV_ather = "4252a21ee4e83f2d4499e89a14f01f1d421a89aa"

DEPENDS += "git autoconf automake pkgconfig gcc libtool libxml2 libssh2 zlib readline openssl openssh ncurses zlib"

S_yuma = "${WORKDIR}/yuma"
S_aether = "${WORKDIR}/aether"

inherit autotools pkgconfig systemd

do_configure:prepend () {
    cp -r ${S_aether}/* ${S_yuma}/example-modules/aether-xcvr
}

do_configure() {
    cd ${S_yuma}
    # Run autoreconf to generate configuration files
    autoreconf -i -f
    # Configure with custom flags
    ./configure CFLAGS='-g -O0' CXXFLAGS='-g -O0' --prefix=${prefix} --host=${TARGET_SYS}
}

do_compile() {
    # Compile the code
    cd ${S_yuma}
    oe_runmake
}

do_install() {
    cd ${S_yuma}
    install -d ${D}${libdir}/yuma
    install -Dm 0644 ${WORKDIR}/startup-cfg.xml ${D}/etc/config/startup-cfg.xml
    install -Dm 0644 ${WORKDIR}/netconfd.conf ${D}/etc/yuma/netconfd.conf
    oe_runmake DESTDIR=${D} install

    # Install the systemd service file
    install -Dm 0644 ${WORKDIR}/netconfd.service ${D}${systemd_system_unitdir}/netconfd.service
}

# Enable the service
SYSTEMD_SERVICE:${PN} = "netconfd.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
FILES:${PN} += "${systemd_system_unitdir}/netconfd.service"
FILES:${PN} += "/etc/config/startup-cfg.xml"
FILES:${PN} += "/etc/yuma/netconfd.conf"
FILES:${PN} += "${libdir}/yuma/*"
FILES:${PN} += "/usr/share/yuma/*"
INSANE_SKIP:${PN} += "la"
INSANE_SKIP:${PN} += "dev-so"