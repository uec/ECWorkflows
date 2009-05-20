#!/usr/bin/perl

use strict;


my $seen_header = 0;


my $pre_low = {};
my $pre_high = {};
my $post_low = {};
my $post_high = {};

my $count_high = 0;
my $count_low = 0;
LINE: while (my $line = <STDIN>)
{
    if (!$seen_header)
    {
	$seen_header = 1;
	next LINE;
    }

    chomp $line;
    my @flds = split(/\t/,$line);

    my $total = $flds[9];
    my $meth = $flds[10];
    
    if ($total >= 5)
    {
	my $meth_frac = $meth / $total;
	my $high = $meth_frac >= 0.75;
	my $low = $meth_frac <= 0.25;

	my $context = $flds[17];

	if (($context !~ /0/) && ($context !~ /CCGG/) && ($high || $low))
	{

	    my $pre = uc(substr($context, 0, 1));
	    my $post = uc(substr($context, 3, 1));

	    if ($high)
	    {
		$count_high++; 
		$pre_high->{$pre}++;
		$post_high->{$post}++;
	    }
	    else
	    {
		$count_low++; 
		$pre_low->{$pre}++;
		$post_low->{$post}++;
	    }
	}
    }
}

# Make High and low versions

my @order = qw/A C G T/;

print ">M1 (High)\n";
#print join("\t", "PO", @order)."\n";
print join("\t", map { $pre_high->{$_}  || "0"} @order)."\n";
print join("\t", (0,$count_high,0,0))."\n";
print join("\t", (0,0,$count_high,0))."\n";
print join("\t", map { $post_high->{$_}  || "0"} @order)."\n";

print ">M2 (Low)\n";
#print join("\t", "PO", @order)."\n";
print join("\t", map { $pre_low->{$_} || "0" } @order)."\n";
print join("\t", (0,$count_low,0,0))."\n";
print join("\t", (0,0,$count_low,0))."\n";
print join("\t", map { $post_low->{$_}  || "0"} @order)."\n";


