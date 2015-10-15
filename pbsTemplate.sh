#PBS -q DAXPBS_QUEUE
#PBS -S /bin/bash
#PBS -l walltime=400:00:00
#DAXPBS_MEM
#DAXPBS_CPU
#Note: DAXPBS comments will be replaced with job specific data
#DAXPBS_DEPS
umask 002
export PATH=/home//primary/vari/software/perl_utils_usc:/primary/vari/software/samtools:$PATH
export CLASSPATH=/primary/vari/software/genomeLibs/apps-live.jar:/primary/vari/software/genomeLibs/biojava-live.jar:/primary/vari/software/genomeLibs/bytecode.jar:/primary/vari/software/genomeLibs/commons-cli.jar:/primary/vari/software/genomeLibs/commons-collections-2.1.jar:/primary/vari/software/genomeLibs/commons-dbcp-1.1.jar:/primary/vari/software/genomeLibs/commons-math-1.1.jar:/primary/vari/software/genomeLibs/commons-pool-1.1.jar:/primary/vari/software/genomeLibs/demos-live.jar:/primary/vari/software/genomeLibs/genomeLibs.jar:/primary/vari/software/genomeLibs/jgrapht-jdk1.5.jar:/primary/vari/software/genomeLibs/junit-4.4.jar:/primary/vari/software/genomeLibs/charts4j-1.2.jar:/primary/vari/software/genomeLibs/heatMap.jar:/primary/vari/software/genomeLibs/UscKeck.jar
export RESULTS_DIR=DAXPBS_RESULTSDIR
export TMP=DAXPBS_TMPDIR
mkdir -p $RESULTS_DIR
mkdir $TMP/$PBS_JOBID
cd $TMP/$PBS_JOBID
#DAXPBS_COPYIN
#DAXPBS_RUN
#DAXPBS_COPYOUT
cd $TMP
rm -rf $PBS_JOBID
#!/bin/sh
echo "Job ID: $1"
echo "User ID: $2"
echo "Group ID: $3"
echo "Job Name: $4"
echo "Session ID: $5"
echo "Resource List: $6"
echo "Resources Used: $7"
echo "Queue Name: $8"
echo "Account String: $9"
echo ""
