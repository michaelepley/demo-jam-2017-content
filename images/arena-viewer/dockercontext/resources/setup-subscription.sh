#!/bin/sh
subscription-manager register

# if no SM_POOL_ID defined, attempt to find the Red Hat employee
# "kitchen sink" SKU (of course, this only works for RH employees)
if [ "x${SM_POOL_ID}" = "x" ]
then
  SM_POOL_ID=`subscription-manager list --available | \
      grep 'Subscription Name:\|Pool ID:\|System Type' | \
      grep -B2 'Virtual' | \
      grep -A1 'Employee SKU' | \
      grep 'Pool ID:' | awk '{print $3}'`

  # exit if none foundsubscription-manager register
  if [ "x${SM_POOL_ID}" = "x" ]
  then
    echo "No subcription manager pool id found.  Exiting"
    exit 1
  fi
fi

# attach the desired pool id
subscription-manager attach --pool="$SM_POOL_ID"

# enable the required repos
subscription-manager repos --disable="*"
subscription-manager repos --enable=rhel-7-server-rpms \
                           --enable=rhel-7-server-optional-rpms \
                           --enable=rhel-7-server-supplementary-rpms \
                           --enable=rhel-7-server-extras-rpms

rpm --import /etc/pki/rpm-gpg/RPM-GPG-KEY-redhat-release

# install EPEL to get the extra packages for guacamole, guacd,
# libguac-client-vnc, and openbox

yum -y localinstall resources/epel-release-latest-7.noarch.rpm