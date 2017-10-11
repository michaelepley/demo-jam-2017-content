#!/bin/sh
set -e

# A single argument can be passed to set the desired screen size.
# 	The argument must be in the form HORZxVERT where HORZ is a
# 	number >= 640 and VERT is a number >= 480.

# default screen size
SCREEN_SIZE="1440x726"

# waits until desired window, with name in $1, is active
function wait_for_window {
	while [ "x`DISPLAY=:1 wmctrl -l | grep "$1"`" = "x" ] ; do sleep 2; done
}

# trap signals so we can close cleanly
RUNNING=true
trap "RUNNING=false" HUP INT QUIT KILL TERM

# set screen size based on arg if valid
if [ "$#" -gt 0 ]
then
  if [ "x`echo $1 | grep -E '^[0-9]+x[0-9]+$'`" != "x" ]
  then
    HORZ=`echo $1 | cut -dx -f1`
    VERT=`echo $1 | cut -dx -f2`

    if [ "$HORZ" -ge 640 -a "$VERT" -ge 480 ]
    then
      SCREEN_SIZE="${HORZ}x${VERT}"
    fi
  fi
fi

# create all needed directories
cd $HOME
mkdir -p .vnc

# set vnc config if missing
cd .vnc
if [ ! -f passwd ]
then
  chmod 740 .
  echo 'VNCPASS' | vncpasswd -f > passwd
  chmod 600 passwd
  echo 'openbox-session &' > xstartup
  chmod a+x xstartup
fi

echo "Screen resolution set to $SCREEN_SIZE"

echo "Launching  Xvnc which launches openbox and applet ..."
cd ..
vncserver :1 -name 'Desktop Name' -geometry $SCREEN_SIZE -depth 24

echo "Making application fullscreen ..."
dummy=$(wait_for_window 'Applet Viewer' && :)
applicationwindow=`DISPLAY=:1 wmctrl -l | grep 'Applet viewer' | cut -d' ' -f1`
DISPLAY=:1 wmctrl -i -r $applicationwindow -b add,fullscreen

echo "Loop to prevent container from exiting"
echo "CTRL-C to exit or run 'docker stop <container>'"
while $RUNNING ; do  sleep 2; done

echo "Closing 'cleanly' by killing Xvnc ..."
vncserver -kill :1

# remove user restricted files so they can be recreated
rm -f passwd .vnc

# set all permissions for any user
dummy=$(chmod -R a+rwX ${HOME}/* && :)

