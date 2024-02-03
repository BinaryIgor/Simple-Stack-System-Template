#!/bin/bash
set -euo pipefail

# Create user and setup passwordless sudo to simplify admin tasks
useradd --create-home --shell "/bin/bash" --groups sudo "${USERNAME}"
echo "${USERNAME} ALL=(ALL) NOPASSWD: ALL" | EDITOR='tee -a' visudo

# Create SSH directory for sudo user and move keys over
home_directory="$(eval echo ~${USERNAME})"
mkdir --parents "${home_directory}/.ssh"
cp /root/.ssh/authorized_keys "$home_directory/.ssh"
chmod 0700 "$home_directory/.ssh"
chmod 0600 "$home_directory/.ssh/authorized_keys"
chown --recursive "${USERNAME}":"${USERNAME}" "$home_directory/.ssh"

# Disable root SSH login with password
sed --in-place 's/^PermitRootLogin.*/PermitRootLogin no/g' /etc/ssh/sshd_config
sed --in-place 's/^PasswordAuthentication.*/PasswordAuthentication no/g' /etc/ssh/sshd_config
if sshd -t -q; then systemctl restart ssh; fi

# Install docker & allow non sudo access
apt update
# install a few prerequisite packages which let apt use packages over HTTPS:
apt install apt-transport-https ca-certificates curl software-properties-common -y
# Then add the GPG key for the official Docker repository to your system
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
# Add the Docker repository to APT sources
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu focal stable"
# This will also update our package database with the Docker packages from the newly added repo.
# Make sure you are about to install from the Docker repo instead of the default Ubuntu repo:
apt-cache policy docker-ce
# Finally, install Docker:
apt install docker-ce -y

# Allow non root access to a docker
usermod -aG docker ${USERNAME}
# limit docker logs size
echo '{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "5"
  }
}' > /etc/docker/daemon.json
# restart docker so that changes can take an effect
systemctl restart docker

# Install prom node-exporter if enabled
if [ "${INSTALL_PROMETHEUS_NODE_EXPORTER}" == "true" ]; then
    PROMETHEUS_NODE_EXPORTER_SLUG="node_exporter-1.5.0.linux-amd64"
    PROMETHEUS_NODE_EXPORTER_URL="https://github.com/prometheus/node_exporter/releases/download/v1.5.0/$PROMETHEUS_NODE_EXPORTER_SLUG.tar.gz"
    # Get and unzip node exporter
    wget "$PROMETHEUS_NODE_EXPORTER_URL"
    tar xvfz "$PROMETHEUS_NODE_EXPORTER_SLUG.tar.gz"
    # Move binary to bin folder
    mv "$PROMETHEUS_NODE_EXPORTER_SLUG/node_exporter" "/usr/local/bin/node_exporter"
    # Remove folder and archive, only binary is needed
    rm "$PROMETHEUS_NODE_EXPORTER_SLUG.tar.gz"
    rm -r "$PROMETHEUS_NODE_EXPORTER_SLUG"
    # Create service file
    echo  "
[Unit]
Description=Exporter of machine stats
StartLimitIntervalSec=0

[Service]
ExecStart=/usr/local/bin/node_exporter
Restart=always
Restart-Sec=10
TimeoutStopSec=10
StandardOutput=journal
StandardError=journal
    
[Install]
WantedBy=multi-user.target" > /etc/systemd/system/node_exporter.service
# Enable service
systemctl daemon-reload 
systemctl enable node_exporter
systemctl start node_exporter
# Print status
systemctl status node_exporter
else
    echo "Skipping prometheus node exporter installation"
fi