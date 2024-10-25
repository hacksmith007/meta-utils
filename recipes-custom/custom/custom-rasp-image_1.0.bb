DESCRIPTION = "Custom image based on core-image-minimal with additional packages"
LICENSE = "MIT"

# Inherit core-image-minimal recipe
require recipes-core/images/core-image-minimal.bb
inherit core-image

IMAGE_INSTALL += " openssh \
    openssh-sftp-server \
    sudo \
    net-tools \
    vim \
    less \
    bash \
    coreutils \
    iproute2 \
    shadow \
    docker-ce \
    yuma123 \ 
    wifi-connect "
# Add additional packages
SYSTEMD_SERVICE_${PN} += "docker.service"