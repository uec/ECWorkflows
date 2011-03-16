#!/usr/bin/perl

# Transforms PSL genomic alignments to gff files.
# ONLY works for gapless hits.

$::INCLUDE_MULTI_HITS = 0;

use strict;

my $names_seen = {};
my $multihits = 0;
LINE: while (my $line = <STDIN>)
{
    chomp $line;
    my @flds_in = split(/\t/,$line);

    my $n = @flds_in[9];
    my $chr = @flds_in[13];
    my $strand = @flds_in[8];
    my ($s,$e) = @flds_in[15..16];

    if ($names_seen->{$n})
    {
	$multihits++;
	next LINE unless ($::INCLUDE_MULTI_HITS);
    }
    $names_seen->{$n}++;

    if ($strand =~ /[\+\-]/)  # Otherwise it's a header line
    {
	my $atts = "gene_id \"$n\"; transcript_id \"$n\";";
	print join("\t",
		   $chr, "PSL", "exon", $s, $e, ".", $strand, ".", $atts);
	print "\n";
    }

}

print STDERR "Found ${multihits} multiple hits\n";

