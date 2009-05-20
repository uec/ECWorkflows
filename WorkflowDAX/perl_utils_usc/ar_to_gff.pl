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
	chomp $line;
	my @flds = split(/\s+/,$line);
	my $src = @flds[0];

	my @out_flds = (@flds[0], "hg17_hum_mus_dog_AR", "region", @flds[1..2], "0.0", ".", ".");

	$src =~ s/chrX/chr23/g;
	$src =~ s/chrY/chr24/g;

	$src_h->{$src} .= join("\t", @out_flds)."\n";
    }
    
    # Close input file
    close(F);

    # And write output files
    SRC: foreach my $src (keys(%$src_h))
    {
	next SRC if (!$src || ($src =~ /^\s*\#/) || ($src =~ /^\s*$/));

	my $str = $src_h->{$src};
	$src =~ s/\/\#\://g;
	my $outfn = $path.$name."_$src".".gff";
	print "Writing to $outfn\n";

	if (1)
	{
	    die "Can't write to $outfn\n" unless (open(OUT,">$outfn"));
	    print OUT $str;
	    close(OUT);
	}
    }
}
