# install java 11
sudo apt install default-jre -y

# install maven
wget https://dlcdn.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz -P /tmp
tar -xvf /tmp/apache-maven-3.9.5-bin.tar.gz
sudo mv apache-maven-3.9.5/ /usr/share/
M2_HOME='/usr/share/apache-maven-3.9.5'
PATH="$M2_HOME/bin:$PATH"
export PATH

# install npm
sudo apt install nodejs -y 
sudo apt install npm -y

# install mysql and start mysql service
sudo apt install mysql-server -y
sudo systemctl start mysql.service