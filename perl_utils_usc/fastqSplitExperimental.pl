#!/usr/bin/perl

use strict;

my $USAGE = "fastqSplit.pl SeqsPerFile seqs1.fastq seqs2.fastq ...";

my ($seqs_per_file, @files) = @ARGV;

if ( (-f $seqs_per_file) || (@files == 0) )
{
    die "$USAGE\n";
}

foreach my $f (@files)
{
    my $file_num = 1;

    my $outc = $f; $outc =~ s/\.(\w+)$/.${file_num}\.$1/g;
    die "Can't write to $outc\n" unless (open(OUTC,">$outc"));

    die "Can't read $f\n" unless (open(IN,$f));

    my $seqs_seen = 0;

    my $linecount_within = 0;
    my $seq_so_far = '';
    my $seq_line = '';
    while (my $line = <IN>)
    {
	if ($line =~ /^\@/)
	{
	    $linecount_within = 1;
	}
	else
	{
	    $linecount_within++;
	}
	
	$seq_so_far .= $line;
	
	if ($linecount_within == 2)
	{
	    $seq_line = $line;
	}
	elsif ($linecount_within == 4)
	{
	    if ($seqs_seen >= $seqs_per_file)
	    {
		# Start a new file
		$file_num++;
		$seqs_seen = 0;

		$outc = $f; $outc =~ s/\.(\w+)$/.${file_num}\.$1/g;
		die "Can't write to $outc\n" unless (open(OUTC,">$outc"));
	    }

	    print OUTC $seq_so_far;
	    
	    $seq_so_far = '';
	    $seq_line = '';

	    $seqs_seen++;
	}
    }

    close (OUTC);    
    close (IN);
}
