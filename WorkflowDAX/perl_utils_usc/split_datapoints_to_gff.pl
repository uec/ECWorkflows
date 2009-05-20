#!/usr/bin/perl

use strict;

#my $PRE = "5x_reg_pot_arsample_intersect";
#my $PRE = "9x_phast_cons_dm4";
my $PRE = "build35_7x_reg_pot_regregions";

my $chr = 0;
while (my $line = <>)
{
    chomp $line;
    # print "line: $line\n";

    if ($line =~ /chrom=(\S+)/)
    {
	my $new_chr = $1;

	if (!$chr || ($chr ne $new_chr))
	{
	    close(F) if ($chr);

# 	$chr =~ s/chrX/chr23/g;
# 	$chr =~ s/chrY/chr24/g;
#	die "\$chr ($chr) !~ chr1 format\n" if ($chr !~ /chr(\d+)/);

	    open(F, ">${PRE}.${new_chr}.gff");
	    print F "\#\#gff-version 3\n";
	}

	$chr = $new_chr;
    }
    elsif (($line =~ /^\s*\#/) || ($line =~ /^\s*$/))
    {
	# Empty
    }
    else
    {
	my ($s, $score) = split(/\s+/,$line);
	print F join("\t", ($chr, "UCSC browser", "region", $s, $s, $score, ".", ".","f"))."\n";
    }
}

close(F) if ($chr);

