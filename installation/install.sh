
# set ssh
ssh-keygen -t rsa -f ~/.ssh/id_rsa -P ''
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys

# install Java, Scala, hadoop 2.7.2, spark-2.0.2
tar -zxvf jdk-8u111-linux-x64.tar.gz
tar -zxvf scala-2.12.0.tgz
tar -zxvf hadoop-2.7.3.tar.gz
tar -zxvf spark-2.0.2-bin-hadoop2.7.tgz

# MODIFY 1: YOUR INSTALLATION FOLDER
export INSTALLATION_HOME=/public/home/tjumalab/shixiangwan

# set environment variable
export JAVA_HOME=$INSTALLATION_HOME/jdk1.8.0_111
export SCALA_HOME=$INSTALLATION_HOME/scala-2.12.0
export HADOOP_HOME=$INSTALLATION_HOME/hadoop-2.7.3
export SPARK_HOME=$INSTALLATION_HOME/spark-2.0.2-bin-hadoop2.7
export PATH=$PATH:$JAVA_HOME/bin:$SCALA_HOME/bin:$SPARK_HOME/bin:$SPARK_HOME/sbin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

# config hadoop, spark
cp -rf config/hdfs-site.xml $HADOOP_HOME/etc/hadoop/hdfs-site.xml
cp -rf config/core-site.xml $HADOOP_HOME/etc/hadoop/core-site.xml
cp -rf config/mapred-site.xml $HADOOP_HOME/etc/hadoop/mapred-site.xml
cp -rf config/yarn-site.xml $HADOOP_HOME/etc/hadoop/yarn-site.xml
cp -rf config/spark-env.sh $SPARK_HOME/conf/spark-env.sh
cp -rf config/start-hadoop-spark.sh ~/start-hadoop-spark.sh

# MODIFY 2: replace the environment variable based on modify 1
sed -i 's;$HADOOP_HOME;/public/home/tjumalab/shixiangwan/hadoop-2.7.3;g' $HADOOP_HOME/etc/hadoop/hdfs-site.xml
sed -i 's;$HADOOP_HOME;/public/home/tjumalab/shixiangwan/hadoop-2.7.3;g' $HADOOP_HOME/etc/hadoop/core-site.xml
sed -i 's;${JAVA_HOME};/public/home/tjumalab/shixiangwan/jdk1.8.0_111;g' $HADOOP_HOME/etc/hadoop/hadoop-env.sh
sed -i 's;${JAVA_HOME};/public/home/tjumalab/shixiangwan/jdk1.8.0_111;g' $SPARK_HOME/conf/spark-env.sh
sed -i 's;${SCALA_HOME};/public/home/tjumalab/shixiangwan/scala-2.12.0;g' $SPARK_HOME/conf/spark-env.sh
sed -i 's;${HADOOP_HOME};/public/home/tjumalab/shixiangwan/hadoop-2.7.3;g' $SPARK_HOME/conf/spark-env.sh

# chmod
chmod +x ~/start-hadoop-spark.sh
chmod +x $HADOOP_HOME/sbin/start-dfs.sh
chmod +x $HADOOP_HOME/sbin/start-yarn.sh
chmod +x $SPARK_HOME/sbin/start-all.sh

# format hdfs
$HADOOP_HOME/bin/hdfs namenode -format
jps
