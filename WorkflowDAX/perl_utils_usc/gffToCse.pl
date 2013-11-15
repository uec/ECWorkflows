#!/usr/bin/perl

use strict;
use File::Basename qw/basename fileparse/;

my @files = @ARGV;

foreach my $f (@files)
{
    die "Can't open $f\n" unless (open(F,$f));

    my $fbase = fileparse($f,qr/\.[^\.]+/);
    my $outf = "${fbase}.cse";
    print STDERR "Out: $outf\n";
    die "Can't write to $outf\n" unless (open(OUTF,">$outf"));


    my $seen = {};
    while (my $line = <F>)
    {
	chomp $line;
	my @flds = split(/\t/,$line);
	print STDERR $line unless (@flds >= 5);

	my $c = @flds[0];
	my $s = @flds[3];
	my $e = @flds[4];

	$c =~ s/chr//ig;
	$c =~ s/X/23/ig;
	$c =~ s/Y/24/ig;
	$c =~ s/M/25/ig;

	my $out = join(",",$c,$s,$e);
	print OUTF $out."\n";
    }

    close (OUTF);
    close(F);
}
