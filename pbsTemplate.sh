#PBS -q DAXPBS_QUEUE
#PBS -S /bin/bash
#PBS -l walltime=48:00:00
#Note: DAXPBS comments will be replaced with job specific data
#DAXPBS_DEPS
export RESULTS_DIR=DAXPBS_RESULTSDIR
export TMP=DAXPBS_TMPDIR
mkdir -p $RESULTS_DIR
mkdir $TMP/$PBS_JOBID
cd $TMP/$PBS_JOBID
#DAXPBS_COPYIN
#DAXPBS_RUN
#DAXPBS_COPYOUT
