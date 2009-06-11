#!/usr/bin/perl

use strict;

my @files = @ARGV;

my $MAX_A_FRAC = 0.9;

foreach my $f (@files)
{
    # Open input
    die "Can't read $f\n" unless (open(IN,$f));

    # Open outputs
    my $outpolya = $f; $outpolya =~ s/\.(\w+)$/.contam.polya\.$1/g;
    die "Can't write to $outpolya\n" unless (open(OUTPOLYA,">$outpolya"));
    my $outadapters = $f; $outadapters =~ s/\.(\w+)$/.contam.adapters\.$1/g;
    die "Can't write to $outadapters\n" unless (open(OUTADAPTERS,">$outadapters"));
    my $outadaptertrim = $f; $outadaptertrim =~ s/\.(\w+)$/.contam.adapterTrim\.$1/g;
    die "Can't write to $outadaptertrim\n" unless (open(OUTADAPTERTRIM,">$outadapters"));
    my $outnoc = $f; $outnoc =~ s/\.(\w+)$/.nocontam\.$1/g;
    die "Can't write to $outnoc\n" unless (open(OUTNOC,">$outnoc"));

    my $linecount_within = 1;
    my $linecount_global = 1;
    my $seq_so_far = '';
    my $seq_line = '';
    while (my $line = <IN>)
    {
	$seq_so_far .= $line;
	
	if ($linecount_within == 1)
	{
	    # Double check the format
	    print STDERR "Incorrect FASTQ file $f\nLine ${linecount_global}: $line\nMod4 lines should start with \@\n"
		unless ($line =~ /^\@/);
	}
	elsif ($linecount_within == 2)
	{
	    $seq_line = $line;
	}
	elsif ($linecount_within == 4)
	{
	    # Solexa adapter sequences. The initial G is optional because sometimes the first cycle is bad
	    # And I don't use it.
	    # The Illumina adapters all have a core sequence GATCGGAAGAG
	    # 1) Illumina adapter 1 (contam001)
            #  example: 18448 GATCGGAAGAGCGGTTCAGCAGGAATGCCGAGATCG
	    # 2) Illumina adapter 2 (contam018)
            #  example: 55788 CGGTTCAGCAGGAATGCCGAGATCGGAAGAGCGGTT
	    # 3) Illumina adapter 3 (contam020)
	    #  example:  1414 CAGCAGGAATGCCGAGATCGGAAGAGCGGTTCAGCA
	    # 4) USC Epigenome Center Me2.0 single end
	    # 5) USC Epigenome Center Me2.X paired end
	    # 6-8) variants of sequence adapters Me2.X
	    if (($seq_line =~ /^G?ATCGGAAGAGCTCG/i) ||   
		($seq_line =~ /^CGGTTCAGCAGGAATG/i) ||		
		($seq_line =~ /^CAGCAGGAATGCCGAG/i) ||		
		($seq_line =~ /^G?TTTGTAAGAG[CT]T[CT]GTA/i) ||		
		($seq_line =~ /^G?TTTGTAAGAG[CT]GGTT[CT]AG[CT]/i) ||
		($seq_line =~ /^[TC]AG[TC]AGGAATG[CT][CT]GAG/i) || # Flavor 2
		($seq_line =~ /^[CT]GGTT[CT]AG[CT]AGGAATG[CT][CT]/i) || # Flavor 3
		($seq_line =~ /^T[CT]GGTT[CT]AG[CT]AGGAATG[CT][CT]/i)) # Flavor 4
	    {
		print OUTADAPTERS $seq_so_far;
	    }
	    # Now check for adapter sequence at end
	    elsif (($seq_line =~ /GATCGGAAGAGCTCG/i) ||
		   ($seq_line =~ /CGGTTCAGCAGGAATG/i) ||		
		   ($seq_line =~ /CAGCAGGAATGCCGAG/i) ||		
		   ($seq_line =~ /GTTTGTAAGAG[CT]T[CT]GTA/i) ||		
		   ($seq_line =~ /GTTTGTAAGAG[CT]GGTT[CT]AG[CT]/i) ||
		   ($seq_line =~ /[TC]AG[TC]AGGAATG[CT][CT]GAG/i) || # Flavor 2
		   ($seq_line =~ /[CT]GGTT[CT]AG[CT]AGGAATG[CT][CT]/i) || # Flavor 3
		   ($seq_line =~ /T[CT]GGTT[CT]AG[CT]AGGAATG[CT][CT]/i)) # Flavor 4
	    {
		print OUTADAPTERTRIM $seq_so_far;
	    }
	    # Should we check for them at the end?
	    else
	    {
		if (aFrac($seq_line) > $MAX_A_FRAC)
		{
		    print OUTPOLYA $seq_so_far;
		}
		else
		{
		    print OUTNOC $seq_so_far;
		}
	    }
	    
	    # Reset our linecount
	    $seq_so_far = '';
	    $seq_line = '';
	    $linecount_within = 0;
	}

	# Increment
	$linecount_within++;
	$linecount_global++;
    }
    
    close (OUTPOLYA);
    close (OUTADAPTERS);
    close (OUTADAPTERTRIM);
    close (OUTNOC);
    close (IN);
}


sub aFrac
{
    my ($seq) = @_;

    chomp $seq;
    $seq = uc($seq);

    my $len = length($seq);
    my $nA = scalar(grep {($_ eq 'A') || ($_ eq 'N')} split(//, $seq));

    my $frac = ($len==0) ? 0 : ($nA/$len);

#    print STDERR join("\t", $seq, $len, $nA, $frac)."\n" if ($frac > $MAX_A_FRAC);
    return $frac;
}
