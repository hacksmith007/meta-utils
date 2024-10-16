DESCRIPTION = "Custom image based on core-image-minimal with additional packages"
LICENSE = "MIT"

# Inherit core-image-minimal recipe
require recipes-core/images/core-image-minimal.bb

IMAGE_INSTALL += " openssh \
    openssh-sftp-server \
    sudo \
    net-tools \
    vim \
    less \
    bash \
    coreutils \
    iproute2 \
    shadow"
# Add additional packages