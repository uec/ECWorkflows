#!/usr/bin/perl

use strict;

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
	print STDERR "fld_map->{". @flds[$fld_map->{POSITION}] . "} = ". @flds[$fld_map->{X}] . ", " . @flds[$fld_map->{Y}] ."\n";
	$xy_map->{ @flds[$fld_map->{POSITION}] } = [@flds[$fld_map->{X}], @flds[$fld_map->{Y}]];
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
	my $xy = $xy_map->{$pos};
	die "Can't find XY for pos $pos\n" unless ($xy);
	my ($x,$y) = @{$xy};
	
	my @atts = split(/\s*;\s*/,$flds[8]);
	push(@atts, "X \"$x\"", "Y \"$y\"");
	$flds[8] = join(";",@atts);
    }

    print join("\t",@flds)."\n";
}
close(GFF);
