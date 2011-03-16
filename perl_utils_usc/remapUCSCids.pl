#!/usr/bin/perl

use strict;


my ($mapfn, @fns) = @ARGV;


# Read in the map
my $map = {};
die "Can't open $mapfn\n" unless (open(MAP, $mapfn));
while (my $line = <MAP>)
{
    chomp $line;
    my ($kgname, $kgchrom, $kgstrand, $kgstart, $kgend, $protid, $symbol, $refseq, $desc) = split(/\t/,$line);
    $map->{$kgname} = $symbol if ($symbol);
}
close(MAP);

foreach my $fn (@fns)
{
    die "Can't read $fn\n" unless (open(F,$fn));
    my $outstr = "";
    while (my $line = <F>)
    {
	if ($line =~ /gene_id\s+\"([^\"]+)\"/)
	{
	    my $id = $1;
	    my $sym = $map->{$id};
	    if ($sym)
	    {
		$line =~ s/$id/${sym}-${id}/g;
	    }
	    $outstr .= $line;
	}
    }
    close(F);

    
    die "Can't write to $fn\n" unless (open(F,">$fn"));
    print F $outstr;
    close(F);
}
