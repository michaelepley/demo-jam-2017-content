FROM fedora:25

MAINTAINER Michael Epley <mepley@redhat.com>

LABEL vendor="Red Hat"
LABEL version="0.1"
LABEL description="JRobots Arena Viewer"

ENV HOME /home/user

# Add the needed packages for guacamole 
RUN    dnf -y update \
    && dnf -y install \
           gettext \
           gtk3 \
           java-1.8.0-openjdk-devel \
           liberation-sans-fonts \
           webkitgtk3 \
           maven \
           nss_wrapper \
           openbox \
           tigervnc-server \
           wmctrl \
           origin-clients \
    && dnf -y clean all

# Create installation directory and set the openbox window manager
# configuration for all users
RUN echo 'export DISPLAY=:1' >> /etc/xdg/openbox/environment
#    && echo "/usr/share/devstudio/devstudio -nosplash -data ${HOME}/workspace &" /etc/xdg/openbox/autostart

# This script starts and cleanly shuts down JBDS and the Xvnc server
ADD resources/start.sh /usr/local/bin/

# This file is used to create a temporary passwd file for use by
# the NSS wrapper so that the openbox window manager can launch
# correctly.  OCP will use a non-deterministic user id, so we have
# to provide a valid passwd entry for that UID for openbox
ADD resources/passwd.template /usr/local/share/

# Create the home directory and set permissions
RUN    mkdir -p ${HOME} \
    && chmod a+rwX ${HOME} \
    && chmod a+rx /usr/local/bin/start.sh \
    && chmod a+r /usr/local/share/passwd.template

EXPOSE 5901

USER 1000

CMD /usr/local/bin/start.sh

# No volume support yet, so everything in /home/jbdsuser is ephemeral.
# Eventually this can be a mounted persistent volume so each user can
# have a persistent maven repository, workspace, etc.
