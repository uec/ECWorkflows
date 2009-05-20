#!/usr/bin/perl

use strict;

my $id = 0;

my $USAGE = "gffyToGff.pl min_count < in.gffy > out.gff";

die "$USAGE\n" unless (@ARGV==1);
my ($min_count) = @ARGV;

LINE: while (my $line = <STDIN>)
{
    chomp $line;
    next LINE unless ($line =~ /^chr/);

    my @flds = split(/\t/,$line);

    next LINE unless ($flds[9] >= $min_count);

    my @out = ();
    
    my $strand = ($flds[16]) ?  "+" : "-";

    push(@out, $flds[0]);
    push(@out, "RRBS");
    push(@out, "exon");
    push(@out, $flds[1]);
    push(@out, $flds[1]+1);
    my $score = ($flds[9]==0) ? 0 : (100*($flds[10]/$flds[9]));
    push(@out, $score);
    push(@out, $strand);
    push(@out, ".");
    
    print join("\t",@out)."\n";
    
    $id++;

}
