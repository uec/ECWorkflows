View the latest version of this document at:
http://epiweb.usc.edu/svn/ECWorkflow/trunk/WorkflowDAX/README.txt?view=markup

##########################
To run pegasus DAX using pbs (with the -pbs flag):
SETUP INPUTS AND PARAMETER FILE
create a dir for your project, copy over your input (fastq) input files. This
dir will end up having all your pbs output. 

copy over the sample workfFlowParams.txt.sample and edit it to your specs:

global settings may only need the tmpDir changed.
Flowcell settings: change the flowcell serial
Lane settings: change the input files, alignment is Bisulfite or regular, and ref genome

######################
OPTIONAL, HIGHLY RECOMENDED:
you are now ready to do a dryrun to verify things are working correctly. it
may take a few minutes since it calculates the number of splits

java -cp /home/uec-00/shared/production/software/ECWorkflow/ECWorkFlow.jar:/home/uec-00/shared/production/software/ECWorkflow/pegasus.jar edu.usc.epigenome.workflow.AlignPileupWorkflow -pbs -dryrun workFlowParams.txt

this will print the pbs commands that will be run. it will also generate a .dax
and .dot file. open the .dot file with graphviz and and view the workflow
graphical plot. graphviz has a nice OSX gui. get graphviz at
http://www.graphviz.org/Download..php

##################
RUNNING THE WORKFLOW
this generates everything, like above, but since there is no "-dryrun" arg, it WILL RUN on
pbs.

java -cp /home/uec-00/shared/production/software/ECWorkflow/ECWorkFlow.jar:/home/uec-00/shared/production/software/ECWorkflow/pegasus.jar edu.usc.epigenome.workflow.AlignPileupWorkflow -pbs workFlowParams.txt

again, this may take a few minutes since we calcuate the number of splits and queue up jobs. dax and
dot files will be generated.  the final results files will be in your tmpDir/FlowcellID (tmpdir that you
specified in your param file). 

#################
TO RUN REPORT GENERATION ON PILEUPS ONLY
this will skip alignments etc, you must have pileups for each lane on hand
make sure the lane inputs in your parameter file reference the pileup.gz files
not the fastq txt files. ex:
Lane.8.Input = s_8_sequence_short.pileup.gz

the only difference is the entry point in the java call:

java -cp /home/uec-00/shared/production/software/ECWorkflow/ECWorkFlow.jar:/home/uec-00/shared/production/software/ECWorkflow/pegasus.jar edu.usc.epigenome.workflow.ReportFromPileup -pbs -dryrun workFlowParams.txt


#######################
LOGGING
A record of the pbs command output will be stored in pbsrun.log.txt or dryrun.log.txt depending on the -dryrun flag respectively. this is the same as what gets sent to stdout
