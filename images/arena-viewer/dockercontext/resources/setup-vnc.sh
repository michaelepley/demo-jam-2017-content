#!/bin/bash

echo "	--> setup the Xvnc password"
mkdir -p ~/.vnc && echo "VNCPASS" | vncpasswd -f > ~/.vnc/passwd && chmod 600 ~/.vnc/passwd && chmod 740 ~/.vnc

# create the xstartup file for Xvnc
cat > ~/.vnc/xstartup <<EOF1
openbox-session &
EOF1

chmod a+x ~/.vnc/xstartup

# set openbox to launch JBDS at startup
mkdir -p ~/.config/openbox ~/workspace

cat > ~/.config/openbox/environment <<EOF2
export DISPLAY=:1
EOF2

cat > ~/.config/openbox/autostart <<EOF3
/usr/share/jbdevstudio/jbdevstudio -nosplash -data $HOME/workspace &
EOF3

