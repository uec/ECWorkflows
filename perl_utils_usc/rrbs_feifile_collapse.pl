#!/usr/bin/perl

use strict;

die "USAGE:: rrbs_feifile_collapse.pl matlab_style dont_collapse_strand < fei_file.txt > collapsed_file.txt\n"
    unless (scalar(@ARGV) >= 2);

my $matlab = shift @ARGV;
my $dont_collapse_strand = shift @ARGV;


# Output these.  they should always be the same except for MspI sites and short reads
my @header = qw/chrom abs_pos rel_pos frag_chr frag_st frag_e frag_len frag_id 1CpG_2CpT_3CpA_4CpC total me fw_me rev_me fw_unme rev_unme smallest_frag strand_dir context/;

my $counts = {};
my $seen_header = 0; # just to speed up
LINE: while (my $line = <STDIN>)
{

    # Skip header
    if (!$seen_header && ($line =~ /^readName/i))
    {
	$seen_header = 1;
	next LINE;
    }
	
    chomp $line;
    my ($read, $frag, $strand, $chr, $abs_pos, $rel_pos, $type, $meth,$context) = split(/\t/,$line);

    my $cpg = 0;
    if ($type eq 'CG')
    {
	$cpg = 1;
    }
    elsif ($type eq 'CT')
    {
	$cpg = 2;
    }
    elsif ($type eq 'CA')
    {
	$cpg = 3;
    }
    elsif ($type eq 'CC')
    {
	$cpg = 4;
    }

	
	my @frag_flds = split(/_/,$frag);
	@frag_flds = map { s/^.*\://g; $_} @frag_flds;
	my ($fchr, $fs, $fe, $fdir, $flen, $fid) = @frag_flds;
	my $fw = ($fdir eq 'FW')?1:0;

	# If it's the "G" of a CpG (i.e. if it's reverse strand), move it back one.
	if (($type eq 'CG') && ($fdir eq 'BW'))
	{
	    $abs_pos--;
	}
		
	my @key_parts = ($chr,$abs_pos);
	push(@key_parts, $fdir) if ($dont_collapse_strand);
	
	my $key = join(":",@key_parts);
	my $prev_counts = $counts->{$key};
	$prev_counts = [$chr, $abs_pos, $rel_pos, $fchr, $fs, $fe, $flen, $fid, $cpg,0,0,0,0,0,0,$flen,$fw,$context]
	unless ($prev_counts);
#	$prev_counts = [$chr, $abs_pos, $rel_pos, $fchr, $fs, $fe, $flen, $fid, $cpg,0,0,0,0,0,0,$flen,$fw,$context,$line]
	
	# Some sanity checks
	if (($fid != @${prev_counts}[7]) && ($rel_pos != 1) && ($flen > 64))
	{
		print STDERR "Following C has two fragment ids ($fid and " . @${prev_counts}[7] .")\n$line\n\n";
	}

	if ( ($cpg != @${prev_counts}[8]) && ($fw == @${prev_counts}[16]) )
	{
		print STDERR "Following C has two types ($cpg/$fw and " . @${prev_counts}[8] ."/".@{$prev_counts}[16].")\n$line\n".@{$prev_counts}[18]."\n\n";
	}
	
	# Now add
	# [$chr, $abs_pos, $rel_pos, frag_chr, frag_st, frag_e, frag len, fid, type, #total, #meth, #fwmeth, #revmeth, #fwunmeth, #fwrevmeth, smallest_frag, $fw, whole_line]
	@${prev_counts}[9]++;
	@${prev_counts}[10]++ if ( $meth );
	
	my $forw = ($fdir eq 'FW');
	@${prev_counts}[11]++ if ( $meth && $forw);
	@${prev_counts}[12]++ if ( $meth && !$forw );
	@${prev_counts}[13]++ if ( !$meth && $forw);
	@${prev_counts}[14]++ if ( !$meth && !$forw);

	@${prev_counts}[15] = $flen if ($flen < @${prev_counts}[15]);
	
	$counts->{$key} = $prev_counts;
}
		
if ($matlab)
{
    my $context_fn = "context.txt";
    die "Can't write to $context_fn\n" unless open(CONTEXT,">$context_fn");
}
else
{
    print join("\t",@header)."\n";
}
	
foreach my $key (sort { sort_count_lists($counts->{$a},$counts->{$b}) }   (keys(%$counts)))
{
	my $count_l = $counts->{$key};
	if ($matlab)
	{
	    my $chr = @{$count_l}[0];
	    $chr =~ s/chr//i;
	    $chr =~ s/X/23/i;
	    $chr =~ s/Y/24/i;
	    $chr =~ s/M/25/i;

	    if ($chr =~ /^(.+)_random/)
	    {
		$chr = $1 + 100;
	    }

	    print join(",",$chr,@{$count_l}[8..16],@{$count_l}[2],@{$count_l}[1])."\n";

	    print CONTEXT @{$count_l}[17]."\n";
	}
	else
	{
		print join("\t",@$count_l)."\n";
	}
}
if ($matlab)
{
    close(CONTEXT);
}


sub sort_count_lists
{
	my ($l1, $l2) = @_;
	
	my $chr1 = @{$l1}[0];
	my $chr2 = @{$l2}[0];
	my $out = sort_chroms($chr1,$chr2);
	
	$out = @{$l1}[1] <=> @{$l2}[1]
		if ($out == 0);

	return $out;
}

sub sort_chroms
{
	my ($c1, $c2) = @_;
	
	return ($c1 cmp $c2);
}

