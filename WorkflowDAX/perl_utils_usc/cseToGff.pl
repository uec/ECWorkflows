#!/usr/bin/perl

use strict;
use File::Basename qw/basename fileparse/;

my @files = @ARGV;

foreach my $f (@files)
{
    die "Can't open $f\n" unless (open(F,$f));

    my $fbase = fileparse($f,qr/\.[^\.]+/);
    my $outf = "${fbase}.gff";
    print STDERR "Out: $outf\n";
    die "Can't write to $outf\n" unless (open(OUTF,">$outf"));


    my $seen = {};
    while (my $line = <F>)
    {
	chomp $line;
	my @flds = split(/,/,$line);
	print STDERR $line unless (@flds == 3);

	my ($c,$s,$e) = @flds;
	$c =~ s/23/X/ig;
	$c =~ s/24/Y/ig;
	$c = "chr$c";

	my $out = join("\t",
		       $c,
		       $fbase,
		       "exon",
		       $s,
		       $e,
		       '.',
		       '+',
		       '.');

	print OUTF $out."\n";
    }

    close (OUTF);
    close(F);
}
