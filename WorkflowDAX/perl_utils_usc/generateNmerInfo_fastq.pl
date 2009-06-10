#!/usr/bin/perl

use File::Basename qw / basename /;
use File::Temp qw/ tempfile tempdir /;
use strict;

my $MIN_QUAL  = 0;
my $MAX_IDENT = 1;
my $USAGE =
  "generateNmerInfo_fastq.pl [-test] [6] [10] [12] file1.fastq file2.fastq ...";

# Parse command line
my $test = 0;
if ( $ARGV[0] =~ /^-t/ )
{
	shift @ARGV;
	$test = 1;
}
my @winds = ();
my $wind;
while ( !( -f ( $wind = shift(@ARGV) ) ) )
{
	push( @winds, $wind );
}
unshift( @ARGV, $wind );
@winds = (6) unless (@winds);

my (@fns) = @ARGV;
die "$USAGE\n" unless ( @fns > 0 );

FILE: foreach my $fn (@fns)
{
	my $base = basename($fn);
	$base =~ s/\.fastq//g;
	$base =~ s/\.txt//g;

	my @cmds = ();

	foreach my $wind (@winds)
	{
		my $outfn = "${base}.${wind}mers.txt";

		# wig file
		push( @cmds,
"fastqToFasta.pl < ${fn} | java -Xmx3995m edu.usc.epigenome.scripts.FastaToNmerCounts -nmer ${wind} > ${outfn}"
		);
	}

	# Run commands
	foreach my $cmd (@cmds)
	{
		print STDERR $cmd . "\n";
		if ( !$test )
		{
			runCmd($cmd);
		}
	}

}

sub runCmd
{
	my ($cmd) = @_;

	my ( $fh, $file ) = tempfile( "generateNmerInfoXXXXXX", DIR => "/tmp" );
	print $fh
"#Run on 1 processors on laird\n#PBS -l walltime=40:00:00\n#PBS -l nodes=1:ppn=1\n#PBS -l mem=3995mb\n#PBS -l arch=x86_64\n#PBS -q laird\n";
	print $fh "cd \"\$PBS_O_WORKDIR\"\n";
	print $fh "${cmd}\n";
	close($fh);

	my $fullcmd = "qsub $file";
	print STDERR "${fullcmd}\n";
	`$fullcmd`;
	unlink($file);
}
