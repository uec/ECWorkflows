#!/usr/bin/perl

use strict;

my $USAGE = "fastaGetSubseq.pl seq-name start-pos end-pos < in.fa > out.fa";

die "$USAGE\n" unless (@ARGV==3);

my ($seq_name, $s,$e) = @ARGV;

my $cur_seq_name = "";
my $cur_seq = "";
while (my $line = <STDIN>)
{
    chomp $line;
    if ($line =~ /^>(.*)/)
    {
	my $new_seq_name = $1;
	
	# Process last one
	print STDERR "Processing $cur_seq_name sequence\n";
	print getSubseq($cur_seq_name, $cur_seq, $s,$e) if ($cur_seq_name =~ /$seq_name/i);
    
	$cur_seq_name = $new_seq_name;
	$cur_seq = "";
    }
    else
    {
	$cur_seq .= $line;
    }
}

# Process final one
print getSubseq($cur_seq_name, $cur_seq, $s,$e) if ($cur_seq_name =~ /$seq_name/i);


sub getSubseq
{
    my ($seq_name, $seq, $start_pos, $end_pos) = @_;

    my $out = ">${seq_name} ${start_pos}-${end_pos}\n";
    $out .= substr($seq,$start_pos-1, $end_pos-$start_pos+1)."\n";

    return $out;
}
