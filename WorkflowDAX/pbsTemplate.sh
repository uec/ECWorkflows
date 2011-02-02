#PBS -q DAXPBS_QUEUE
#PBS -S /bin/bash
#PBS -l walltime=300:00:00
#DAXPBS_MEM
#Note: DAXPBS comments will be replaced with job specific data
#DAXPBS_DEPS
umask 022
export PATH=$PATH:/home/uec-00/shared/production/software/pegasus/2.2.0/bin:/home/uec-00/shared/production/software/perl_utils_usc:/home/uec-00/shared/production/software/maq-0.7.1:/home/uec-00/shared/production/software/bin:/usr/bin:/bin:/usr/sbin:/sbin:/home/rcf-40/bberman/storage/bin:/home/uec-00/shared/production/software/tophat/default:/home/uec-00/shared/production/software/bowtie/default:/home/uec-00/shared/production/software/cufflinks/default:/home/uec-00/shared/production/software/samtools
export CLASSPATH=/home/uec-00/shared/production/software/genomeLibs/apps-live.jar:/home/uec-00/shared/production/software/genomeLibs/biojava-live.jar:/home/uec-00/shared/production/software/genomeLibs/bytecode.jar:/home/uec-00/shared/production/software/genomeLibs/commons-cli.jar:/home/uec-00/shared/production/software/genomeLibs/commons-collections-2.1.jar:/home/uec-00/shared/production/software/genomeLibs/commons-dbcp-1.1.jar:/home/uec-00/shared/production/software/genomeLibs/commons-math-1.1.jar:/home/uec-00/shared/production/software/genomeLibs/commons-pool-1.1.jar:/home/uec-00/shared/production/software/genomeLibs/demos-live.jar:/home/uec-00/shared/production/software/genomeLibs/genomeLibs.jar:/home/uec-00/shared/production/software/genomeLibs/jgrapht-jdk1.5.jar:/home/uec-00/shared/production/software/genomeLibs/junit-4.4.jar:/home/uec-00/shared/production/software/genomeLibs/charts4j-1.2.jar:/home/uec-00/shared/production/software/genomeLibs/heatMap.jar:/home/uec-00/shared/production/software/genomeLibs/UscKeck.jar
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