#!/usr/bin/perl

use strict;

# CONSTANTS
my $USAGE = "agilentCoordGeneFilesToGff.pl coordFile geneFile";

my ($coord_fn, $gene_fn) = @ARGV;

my $coords = [];
my $genes = [];
foreach my $coord_loop (0,1)
{
    my $fn = ($coord_loop) ? $coord_fn : $gene_fn;
    die "Can't read $fn\n" unless (open(F, $fn));
    my $l = ($coord_loop) ? $coords : $genes;
    while (my $line = <F>)
    {
	my $data = ($coord_loop) ? readCoordLine($line) : readGeneLine($line);
	push(@$l, $data) if ($data);
    }
    close(F);
}

my $n_coords = scalar(@$coords);
my $n_genes = scalar(@$genes);
print STDERR "Found $n_coords coords and $n_genes genes\n";

for (my $i=0; $i < $n_coords; $i++)
{
    my $g = @{$genes}[$i];
    my $coordl = @{$coords}[$i];
    my ($chr, $s, $e) = @${coordl};

    my $track = ($i+1); # "agilent";
    my $atts = "gene_id \"$g\"; transcript_id \"$g\";";
    print join("\t", ($chr, $track, "exon", sprintf("%d\t%d",$s, $e), ".", "+", ".",$atts))."\n";

}


# Returns ($gene)
sub readGeneLine
{
    my ($line) = @_;
    chomp($line);

    return $line;
}

# Returns ($chr, $s, $e);
sub readCoordLine
{
    my ($line) = @_;
    my $out = 0;

    chomp($line);
    if ($line =~ /(chr\w+)\:(\d+)\-(\d+)/)
    {
	$out = [$1,$2,$3];
    }
    else
    {
	print STDERR "Illegal coord line: $line\n";
    }

    return $out;
}
