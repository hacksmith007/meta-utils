DESCRIPTION = "Recipe to install Netconf Server and yangCliPro"
HOMEPAGE = "https://yoctoproject.org"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI_yuma ?= "github.com/hacksmith007/openyuma"
SRC_URI_aether_fwd_yuma ?= "/home/rahul/yocto/yocto-sources/aether/forwardplane"
SRC_URI_aether_yang ?= "/home/rahul/yocto/yocto-sources/aether/yang"

PROTOCOL_aether ?= "https"
PROTOCOL_yuma ?= "https"
PROTOCL_forwardplane ?= "https"

# Prepare task: Copies from local folder to ${WORKDIR}
python do_prepare() {
    import shutil
    import os
    src_uri_fwd_yuma = d.getVar("SRC_URI_aether_fwd_yuma")
    src_uri_yang = d.getVar("SRC_URI_aether_yang")

    if os.path.exists(src_uri_fwd_yuma):
        bb.warn(f"❌ Cloning forwardplane from local")
        d.setVar("PROTOCL_forwardplane", "file")
    else:
        d.setVar("SRC_URI_aether_fwd_yuma", "git@github.com:hacksmith007/forwardplane.git")
        bb.warn(f"❌ Cloning forwardplane from remote")

    if os.path.exists(src_uri_yang):
        d.setVar("PROTOCL_yang", "file")
    else:
        d.setVar("SRC_URI_aether_yang", "git@github.com:hacksmith007/yang.git")
        bb.warn(f"❌ Cloning yang from remote")
}

addtask prepare before do_patch after do_fetch

BRANCH_yuma = "alpha_devR1.0"
BRANCH_fwd = "devR1.0"
BRANCH_yang = "devR1.0"


SRC_URI = "git://${SRC_URI_yuma};protocol=${PROTOCOL_yuma};branch=${BRANCH_yuma};name=yuma;destsuffix=yuma \
           git://${SRC_URI_aether_fwd_yuma};protocol=${PROTOCOL_yuma};branch=${BRANCH_fwd};name=forwardplane;destsuffix=forwardplane \
           git://${SRC_URI_aether_yang};protocol=${PROTOCOL_aether};branch=${BRANCH_yang};name=ather;destsuffix=aether"

SRC_URI += "file://netconfd.service \
            file://netconfd.conf \
            file://startup-cfg.xml "


SRCREV_yuma = "7aab0e24e4e828a121314904f4a646c8ad6c7a1e"

DEPENDS += "git autoconf automake pkgconfig gcc libtool libxml2 libssh2 zlib readline openssl openssh ncurses zlib"

S_yuma = "${WORKDIR}/yuma"
S_yang = "${WORKDIR}/aether"
S_forwardplane = "${WORKDIR}/forwardplane"

inherit autotools pkgconfig systemd

do_configure:prepend () {
    cp -r ${S_yang}/* ${S_yuma}/example-modules/aether-xcvr
    mkdir ${S_yuma}/example-modules/aether-networks
    cp -r ${S_forwardplane}/yuma/aether-xcvr/* ${S_yuma}/example-modules/aether-xcvr
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