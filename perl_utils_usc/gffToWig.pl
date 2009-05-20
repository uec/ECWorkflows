#!/usr/bin/perl

use strict;
use File::Basename qw/basename fileparse/;

my @files = @ARGV;

foreach my $f (@files)
{
    die "Can't open $f\n" unless (open(F,$f));

    my $fbase = fileparse($f,qr/\.[^\.]+/);
    my $outf = "${fbase}.wig";
    print STDERR "Out: $outf\n";
    die "Can't write to $outf\n" unless (open(OUTF,">$outf"));



    while (my $line = <F>)
    {
	chomp $line;
	my @flds = split(/\t/,$line);
	print STDERR $line unless (@flds >= 5);

	my $c = @flds[0];
	my $s = @flds[3];
	my $e = @flds[4];
	my $score = @flds[5];

	my $out = join(",",$c,$s,$e);
	print OUTF $out."\n";
    }

    close (OUTF);
    close(F);
}
