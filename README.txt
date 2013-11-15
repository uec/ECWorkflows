View the latest version of this document at:
http://epiweb.usc.edu/svn/ECWorkflow/trunk/WorkflowDAX/README.txt?view=markup

##########################
Overview
This document will explain that how to run workflows at USC HPCC. Input files, arguments, and execution are covered below.

##########################
Workflows Implemented so far:


edu.usc.epigenome.workflow.SequencingPipeline
A wrapper for other application specific workflow, starts from per-lane fastq files, does alignment and reporting based upon whether the lane is regular, Bisulfite or chipseq, which is specified in the param file:
EX:
Lane.2.Input = s_6_sequence_short.txt #(input fastq file, can be symlink)
Lane.2.AlignmentType = bisulfite      #(could be bisulfite, chipseq or regular)
Lane.2.ReferenceBFA = /home/uec-00/shared/production/genomes/hg18_unmasked/hg18_unmasked.plusContam.bfa


edu.usc.epigenome.workflow.generator.ReportFromPileup
starts from per-lane Pileup.gz files and generates lane-wise reports

edu.usc.epigenome.workflow.generator.ReportFromMaps
starts from map files, merges and does reports.


#########################
Arguments:

-pbs
causes the workflow to be executed in pbs mode, otherwise only an xml file will be generated (for use with pegasus). This will cause the the workflow to generate the proprocessing needed to execute directly in PBS  

-dryun
if specfied, no execution actually takes place, you can use this to verify that the workflow is generating the correct dependencies and commands. this only makes sense when used with the -pbs flag

-linear
run each samples job one-by-one on a single node

parameterFile.txt
you may pass in a parameter file that contains experimental input settings (lane settings, flowcell ID etc..)

http://processURI
you may pass in a url to the genologics geneus process remote xml. parameters will be set using the process settings from geneus

##########################
Retrieving Parameters
Either a parameter file, or parameter URL, or BOTH can be specified on the command line. If any required parameters are missing they will be set to internal defaults. If both are specified, then the URL will have precedence.

##########################
Input Files
the workflow allows input parameters to be specified as either input fastq seqs (for AlignPileupWorkflow workflow), or input pileup.gz files (for ReportFromPileup workflow).
these are specified in the paramters file. if not specified, the workflow will guess the input files by looking in the current dir, and matching files "*s_$n*" where $n is the lane number 

###########################
Workflow Output files

workflowParamsUsed.log.txt
the parameters used are written to this file

pbsrun.log.txt
contains all pbs code executed when in -pbs mode (with no -dryrun)

dryrun.log.txt
contains pbs code what WOULD HAVE BEEN executed

various *.dot files
use graphviz to view graphical flow charts of the workflow. 
http://www.graphviz.org/Download..php

various xml files 
the xml file to be used with pegasus, created per workflow-type (ex chipseq, bs or regular will be grouped into a single workflow)

##################################
Analysis Output
results will be stored in your tmpDir/FlowcellID. (both these will come from your parameter file)

#####################################
Execution Examples
its always recommended to do a dryrun (-dryrun) before really executing on the cluster.

do a test run (dryrun) from input fastq seqs using file only:
java -cp /home/uec-00/shared/production/software/ECWorkflow/ECWorkFlow.jar:/home/uec-00/shared/production/software/ECWorkflow/pegasus.jar edu.usc.epigenome.workflow.SequencingPipeline -pbs -dryrun workFlowParams.txt

do a pbs run  from input fastq seqs using file and http://processURL :
java -cp /home/uec-00/shared/production/software/ECWorkflow/ECWorkFlow.jar:/home/uec-00/shared/production/software/ECWorkflow/pegasus.jar edu.usc.epigenome.workflow.SequencingPipeline -pbs workFlowParams.txt http://epilims.usc.edu:8080/api/processes/GW2-BPB-090630-24-547

run the report-only workflow using pileup inputs using a paramter file only:
java -cp /home/uec-00/shared/production/software/ECWorkflow/ECWorkFlow.jar:/home/uec-00/shared/production/software/ECWorkflow/pegasus.jar edu.usc.epigenome.workflow.generator.ReportFromPileup -pbs workFlowParams.txt

run the report-only workflow using pileup inputs using a http://processURL only
java -cp /home/uec-00/shared/production/software/ECWorkflow/ECWorkFlow.jar:/home/uec-00/shared/production/software/ECWorkflow/pegasus.jar edu.usc.epigenome.workflow.generator.ReportFromPileup -pbs  http://epilims.usc.edu:8080/api/processes/GW2-BPB-090630-24-547

run the report-only workflow using pileup inputs using a paramter file only:
java -cp /home/uec-00/shared/production/software/ECWorkflow/ECWorkFlow.jar:/home/uec-00/shared/production/software/ECWorkflow/pegasus.jar edu.usc.epigenome.workflow.generator.ReportFromPileup -pbs workFlowParams.txt

run the report-only workflow using pileup inputs using a http://processURL only
java -cp /home/uec-00/shared/production/software/ECWorkflow/ECWorkFlow.jar:/home/uec-00/shared/production/software/ECWorkflow/pegasus.jar edu.usc.epigenome.workflow.generator.ReportFromPileup -pbs  http://epilims.usc.edu:8080/api/processes/GW2-BPB-090630-24-547

run the report-only workflow using map inputs with no paramfile (since probably not needed):
java -cp /home/uec-00/shared/production/software/ECWorkflow/ECWorkFlow.jar:/home/uec-00/shared/production/software/ECWorkflow/pegasus.jar edu.usc.epigenome.workflow.generator.ReportFromMaps -pbs file1.map file2.map file3.map 

######################################
Global Defaults
aside from flowcellID (which MUST be passed in from either param file or URL), all other paramters will be retrieved from /auto/uec-00/shared/production/software/ECWorkflow/workFlowParamsGlobalDefaults.txt if NOT already specified
