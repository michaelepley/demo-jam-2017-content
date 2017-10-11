#!/bin/bash

echo "Sets up VNC"
echo "	--> setup the Xvnc password"
mkdir -p ~/.vnc && echo "VNCPASS" | vncpasswd -f > ~/.vnc/passwd && chmod 600 ~/.vnc/passwd && chmod 740 ~/.vnc

# create the xstartup file for Xvnc
echo 'openbox-session &' > ~/.vnc/xstartup

chmod a+x ~/.vnc/xstartup

mkdir -p ~/.config/openbox

echo 'export DISPLAY=:1'  > ~/.config/openbox/environment
# set openbox to launch the normal java s2i entrypoint at startup
echo '/opt/s2i-java/run-java.sh &' > ~/.config/openbox/autostart

