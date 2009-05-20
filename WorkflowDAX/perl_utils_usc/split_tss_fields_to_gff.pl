#!/usr/bin/perl

use strict;
use File::Basename;
use List::Util qw(min max);


# Go through lines
 LINE: while (my $line = <STDIN>)
{
    chomp $line;
    next LINE if (($line =~ /^\#/) || ($line =~ /^\s*$/));

#    my ($n, $chr, $strand, $s, $e) = split(/\t/,$line);
    my ($strand, $n, $chr, $s, $e) = split(/\t/,$line);
    print STDERR $line."\n" if ($s > $e);

    print join("\t",
	       $chr,
#	       "knownGene-TSS",
	       "allMrna-TSS",
	       "exon",
	       ($strand eq '+')?$s:$e,
	       ($strand eq '+')?$s:$e,
	       '.',
	       $strand,
	       '.',
	       "gene_id \"$n\"; transcript_id \"$n\";");
    print "\n";
}
