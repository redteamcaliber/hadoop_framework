#!/bin/sh

if [ $# -ne 2 ]
  then
  echo "Usage: tpkickoff.sh image_friendly_name image_path"
  exit 1
fi

pwd

JarFile=`ls pipeline/target/sleuthkit-pipeline-r*-job.jar | sort | tail -n 1`

FriendlyName=$1
ImagePath=$2

JsonFile=$FriendlyName.json
HdfsImage=$FriendlyName.dd

echo "jar file is ${JarFile}"

# rip filesystem metadata, upload to hdfs
fsrip dumpfs $ImagePath | $HADOOP_HOME/bin/hadoop jar $JarFile com.lightboxtechnologies.spectrum.Uploader $JsonFile

# upload image to hdfs
ImageID=`cat $ImagePath | $HADOOP_HOME/bin/hadoop jar $JarFile com.lightboxtechnologies.spectrum.Uploader $HdfsImage`

# kick off ingest
$HADOOP_HOME/bin/hadoop jar $JarFile org.sleuthkit.hadoop.pipeline.Ingest $ImageID $HdfsImage $JsonFile