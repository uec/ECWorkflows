#!/usr/bin/perl

use strict;

# Gfffn is a template GFF file used for the ordering.  We 
# replace the score with the score in the XY file.
#
# We map by POSITION field in XY file compared to "start"
# field in the gff template file
my ($gfffn, $xyfn) = @ARGV;


die "Can't open $xyfn\n" unless (open(XY,$xyfn));
my $fld_map;
my $xy_map = {};
my $on_line = 1;
LINE: while (my $line=<XY>)
{
    chomp $line;
    my @flds = split(/\t/,$line);
    if ($line =~ /PROBE_ID/i)
    {
	$fld_map = {};
	for (my $i = 0; $i < scalar(@flds); $i++)
	{
	    $fld_map->{uc(@flds[$i])} = $i;
	}
    }
    else
    {
#	print STDERR "fld_map->{". @flds[$fld_map->{POSITION}] . "} = ". @flds[$fld_map->{PM}] ."\n";
	$xy_map->{ @flds[$fld_map->{POSITION}] } = @flds[$fld_map->{PM}];
    }
    
    $on_line++;
}
close(XY);

die "Can't open $gfffn\n" unless (open(GFF,$gfffn));
while (my $line=<GFF>)
{
    chomp $line;
    my @flds = split(/\t/,$line);
    my $pos = $flds[3];

    if ($pos =~ /^\d+$/)
    {
	my $score = $xy_map->{$pos};

	die "Can't find score for pos $pos\n" unless ($score);
	
	@flds[2] = $xyfn;
	@flds[5] = $score;

	print join("\t",@flds)."\n";
    }

}
close(GFF);
