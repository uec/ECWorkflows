#!/usr/bin/perl

use strict;

my $id = 0;
print "track name='RRBS (brown=meC)' itemRgb='on' visibility=4\n";

while (my $line = <>)
{
    chomp;

    my @flds = split(/\t/,$line);

    if ($flds[6] eq 'CG')
    {
    
    my @out = ();

    my $strand = ($flds[1] =~ /BW/) ? "-" : "+";
    my $meth = ($flds[7]) ? "128,0,0" : "0,128,0";

    push(@out, $flds[3]);
    push(@out, $flds[4]);
    push(@out, $flds[4]+1);
    push(@out, $id);
    push(@out, "1.0");
    push(@out, $strand);
    push(@out, $flds[4]);
    push(@out, $flds[4]+1);
    push(@out, $meth);

    print join("\t",@out)."\n";

    $id++;
     }
}
