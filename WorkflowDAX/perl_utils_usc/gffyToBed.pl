#!/usr/bin/perl

use strict;

my ($MIN_COUNT) = @ARGV;

my $id = 0;
print "track name='RRBS' visibility=4 itemRgb='on'\n";

# chr17   34500   34531   A3429092   1.0   -   34500   34531   255,0,0   




while (my $line = <STDIN>)
{
    chomp;
    next if ($line =~ /abs_pos/); # Header

    my @flds = split(/\t/,$line);

    if ($flds[8] == 1)
    {
    
    my @out = ();

    my $strand = ($flds[16]) ?  "+" : "-";

    push(@out, $flds[0]);
    push(@out, $flds[1]);
    push(@out, $flds[1]+1);
    push(@out, $id);
    my $score = ($flds[9]==0) ? 0 : (1000*($flds[10]/$flds[9]));
    push(@out, $score);
    push(@out, $strand);

    # 
    my $color;
    if ($flds[9] < $MIN_COUNT)
    {
	$color  = "169,169,169";
    }
    elsif ($score < 250)
    {
	$color = "0,255,0";
    }
    elsif ($score > 750)
    {
	$color = "255,0,0";
    }
    else
    {
	$color = "0,0,255";
    }

    push(@out, $flds[1]);
    push(@out, $flds[1]+1);
    push(@out,$color);


    print join("\t",@out)."\n";

    $id++;
     }
}
