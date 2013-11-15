#!/usr/bin/env perl

use strict;

my $USAGE = "maqToBed.pl file1.map.q30.txt file2.map.q30.txt ...";
die "$USAGE\n" unless (@ARGV>0);

foreach my $fn (@ARGV)
{
    my $base = $fn;
    $base =~ s/\.map.*//g;
    my $outfn = "${base}.bed";

    print STDERR "$fn -> $outfn\n";
    die "Can't read $fn\n" unless (open(IN,$fn));
    die "Can't write to $outfn\n" unless (open(OUT,">$outfn"));

    print OUT "track name='${base}' visibility=4 itemRgb='on'\n";
    my $on_line = 0;
    LINE: while (my $line = <IN>)
    {
	chomp $line;
	my @flds = split(/\t/,$line);
	my $name = $on_line; # $flds[0]
	my $chrom = $flds[1];
	next LINE unless ($chrom =~ /^chr/);

	my $s = $flds[2];
	my $strand = $flds[3];
	my $score = $flds[6];
	my $len = $flds[13];

	my $e = $s + $len - 1;
	my $color = ($strand eq '+') ? "0,0,255" : "255,0,0";

	my @out_flds = ( $chrom, $s, $e, $name, $score, $strand, $s, $e, $color);
	print OUT join("\t",@out_flds)."\n";

	$on_line++;
    }

    close(OUT);
    close(IN);

    `bzip2 -f $outfn`;

}
