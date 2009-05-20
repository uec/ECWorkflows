#!/usr/bin/env perl

use strict;


LINE: while (my $line = <STDIN>)
{
    next LINE if ($line=~/^\#/);

    chomp $line; 
    my @flds = split(/\t/,$line);
    my ($id, $chr, $strand, $n_exons, $starts, $ends, $name) = @flds;

    my @start_flds = split(/,/,$starts);
    my @end_flds = split(/,/,$ends);

    for (my $i = 0; $i < $n_exons; $i++)
    {
	my $s = $start_flds[$i];
	my $e = $end_flds[$i];
	print join("\t",
		   $chr,
		   "knownGeneExon",
		   "exon",
		   $s,
		   $e,
		   "0.0",
		   $strand,
		   "0",
		   "gene_id \"${name}\"; transcript_id \"${name}\";");
    }
}
