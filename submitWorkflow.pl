#!/usr/bin/perl


my $workflowCmd = "java -Xms4096m -Xmx4096m -cp /home/uec-00/shared/production/software/ECWorkflow/ECWorkFlow.v4.jar:/home/uec-00/shared/production/software/ECWorkflow/pegasus.jar edu.usc.epigenome.workflow.SequencingPipeline -pbs ";

my $workflow = shift @ARGV;
-e $workflow || die "workflow not found";
checkParam($workflow);

$otherArgs = join(" ",@ARGV);

open(IN,"$workflow") || die;
@lines = <IN>;
close IN;

@header = grep {!/Sample/} @lines;
@lines = grep /Sample/, @lines;


$SIG{'INT'} = sub {die("Ctrl-C detected! BAILING! you can continue where you left off by using $workflow\.incomplete")};

my $current_sample = -1;
for $line (@lines)
{
	if($line =~ /^\#/)
	{
		push @samples, $sample_entry if $sample_entry;
		$sample_entry = $line;	
		$current_sample = -1;
	}
	elsif($line =~ /ample\.(\d+)\./)
	{
		my $id = $1;
		if($current_sample == $id || $current_sample == -1)
		{
			$sample_entry .= $line;	
			$current_sample = $id;
		}
		else
		{
			push @samples, $sample_entry if $sample_entry;
			$sample_entry = $line;	
			$current_sample = $id;
		}
	}
	else { die "unexpected line: $line";}
}
push @samples, $sample_entry if $sample_entry;


while(@samples)
{
	writeSample("$workflow\.incomplete",@samples);	
	writeSample("$workflow\.current", $samples[0]);
	print STDERR "Submitting Sample: " . @{[split("\n",$samples[0])]}[0] . "\n" ;
	`$workflowCmd $workflow\.current $otherArgs`;
	shift @samples;
}

unlink("$workflow\.current");
unlink("$workflow\.incomplete");




sub writeSample
{
	my $fileName = shift @_;
	unlink($fileName);
	return unless @_;
	my @linesWrite = (@header,@_);
	open(OUT, ">$fileName");
	for (@linesWrite)
	{
		print OUT "$_\n" unless $_ =~ /^\s+$/;
	}
	close OUT;
}


sub checkParam
{
	my $file = shift @_;
	die "Param filename should look someting like \"workFlowParams*\", $file not valid name" unless $file =~ /aram/;
	open(INCHECK, "<$file");
	my $foundCS;
	my $foundFC;
	my $foundQ;

	my %uniqParams;
	while(my $line = <INCHECK>)
	{
		$foundCS = 1 if $line =~ /ClusterSize\s*\=\s*\d+/;
		$foundFC = 1 if $line =~ /FlowCellName\s*\=\s*\S+/ && $line !~ /_/;
		$foundQ = 1 if $line =~ /queue\s*\=\s*\S+/;

		if($line =~ /^\s*(.+?)\s*\=\s*(.+)\s*$/)
		{
			my $param = $1;
			my $val = $2;
			print "$param is defined multiple times in file\n" if  $uniqParams{$param};
			$uniqParams{$param} ++;
			if($param =~ /Sample.+input/i)
			{
				my @files = split(",", $val);
				-e $_ || warn "INPUT FILE \t $_ \tspecified in params file but not found!\n" for @files;
			}

			if($param =~ /Sample.+efer/i)
			{
				-e $val || -e "$val.fa" ||  die "REFERENCE GENOME \t $val  \t specified in param file but not found!\n";
			}
		}
	}
	die "MISSING ClusterSize! (ex: ClusterSize = 1)\n" if(!$foundCS);
	die "MISSING/INCORRECT FlowCellName! (ex: FlowCellName = ZXC123XX)\n" if(!$foundFC);
	die "MISSING queue! (ex: queue = laird)\n" if(!$foundQ);	
	close INCHECK;
}
