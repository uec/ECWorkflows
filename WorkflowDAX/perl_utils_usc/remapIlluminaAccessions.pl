#!/usr/bin/perl
#
# Special application.  genes file contains a list of refseq accessions, possibly
# transcript level (i.e. NM_012088.2).  We map these to our special illumina_TSS
# file which is a gff file where probes are mapped sometimes to the canonical
# gene (i.e. NM_012088) and sometimes to the transcript ID.  We also may have
# a prefix , "ILMN00000--"
# 
# For each line in the genes file, we return either 0 (if it was not found in
# the ill_tss file) or the index of the gene's position in the ill_tss file.

use strict;
use File::Basename qw/basename/;


my $USAGE = "remapAccessions.pl genes.txt ill_tss.gff";

die "$USAGE\n" if (@ARGV != 2);

my ($genes_fn, $ill_tss_fn) = @ARGV;

my ($genes_fn, $ill_tss_fn, $chip_gff_fn, $boundary_gff_fn) = @ARGV;


# Get the basename from diff file
my $base = $genes_fn;
$base =~ s/\.\w+$//g;
print STDERR "Base: $base\n";

# First go through genes , making a link to positions
die "Can't read $genes_fn\n" unless (open(F,$genes_fn));
my $gene_positions = {};
my $on_element = 1;
while (my $line = <F>)
{
    next if ($line =~ /^\s*$/);

    chomp $line;
    my $id = uc($line);
    $gene_positions->{$id} = $on_element;
#   print STDERR "${on_element}: $id\n";

    # And without the transcript identifier
    if ($id =~ /^(.+)(\.\d+)$/)
    {
	my $stripped = $1;
#	print STDERR "${on_element}: \t$stripped\n";
	$gene_positions->{$stripped} = $on_element;
    }

    $on_element++;
}
close(F);


# Generate the positions of all the TSSs
die "Can't read $ill_tss_fn\n" unless (open(TSS, $ill_tss_fn));
my $on_element = 1;
while (my $line = <TSS>)
{
    next unless ($line =~ /gene_id\s+\"(?:[^\"]+\-\-)?([^\.\"]+)(\.[^\"]+)?\";/);
    my $gid = uc($1);
    my $tx = uc($2);

    my $ind = 0;

    # See if the full transcript is there
    $ind = $gene_positions->{"${gid}${tx}"};
    
    # If not, check for the stripped version
    if ($ind)
    {
#	print STDERR "TX:\t$gid$tx\n";
    }
    else
    {
	$ind = $gene_positions->{$gid};

	if ($ind)
	{
	    print STDERR "GENE:\t$gid$tx\n";
	}
	else
	{
	    $ind = '0';
	    print STDERR "NONE:\t$gid$tx\n";
	}
    }

    print $ind."\n";
    
    # Update position
    $on_element++;
}
close(TSS);








