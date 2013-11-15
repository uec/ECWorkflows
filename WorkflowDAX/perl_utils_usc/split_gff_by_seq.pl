#!/usr/bin/perl

use strict;
use File::Basename;

my @files = @ARGV;


foreach my $f (@files)
{

    my ($name, $path, $suf) = fileparse($f, qr/\.[^.]*/);
    print "($name)\t($path)\t($suf)\n";

    # Keep output strings in a hash
    my $src_h = {};

    die "Couldn't read $f\n" unless (open(F,$f));
    while (my $line = <F>)
    {
	if ($line =~ /^\s*\#/ || $line =~ /^\s*$/ || $line =~ /track name/)
	{
	}
	else
	{
	    my @flds = split(/\t/,$line);
	    my $src = @flds[0];
	    
	    $src =~ s/chrX/chr0/g;
	    $src =~ s/chrY/chr24/g;
	    
	    $src =~ s/chr2L/chr1/g;
	    $src =~ s/chr2R/chr2/g;
	    $src =~ s/chr3L/chr3/g;
	    $src =~ s/chr3R/chr5/g;

	    $src_h->{$src} .= $line;
	}
    }
    
    # Close input file
    close(F);

    # And write output files
    SRC: foreach my $src (keys(%$src_h))
    {
	next SRC if (!$src || ($src =~ /^\s*\#/) || ($src =~ /^\s*$/));

	my $str = $src_h->{$src};
	$src =~ s/\/\#\://g;
	my $outfn = $path.$name."_$src".$suf;
	print "Writing to $outfn\n";

	if (1)
	{
	    die "Can't write to $outfn\n" unless (open(OUT,">$outfn"));
	    print OUT $str;
	    close(OUT);
	}
    }
}
