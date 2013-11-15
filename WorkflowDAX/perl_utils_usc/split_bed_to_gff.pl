#!/usr/bin/perl

use strict;


my ($prefix, @fns)  = @ARGV;


foreach my $fn (@fns)
{
    die "Couldn't open $fn" unless (open(INFILE,$fn));

    my $track = 0;
    my $el = 1;
    while (my $line = <INFILE>)
    {
	chomp $line;
	# print "line: $line\n";
	
	if ($line =~ /name=[\"\']?([^\"\'\ ]+)[\"\']?/)
	{
	    close(F) if ($track);
	    $track = $1;
	    open(F, ">${prefix}.${track}.gff");
	    $el = 1;
	    #print F $line."\n";
	    # print F "\#\#gff-version 3\n";
	}
	elsif (($line =~ /^\s*\#/) || ($line =~ /^\s*$/))
	{
	    # Empty
	}
	else
	{
	    my ($chr, $s, $e, $blk, $score) = split(/\s+/,$line);
#	    $blk = "${track}.${chr}.${s}" unless ($blk);
	    $blk = "${track}.${el}" unless ($blk);
# 	    $chr =~ s/chrX/chr23/g;
# 	    $chr =~ s/chrY/chr24/g;
#	    die "\$chr ($chr) !~ chr1 format\n$line\n" if ($chr !~ /chr(\d+)/);
	    my $chr_num = $1;
	    # print F join(",", ($chr_num, $s, $e, $score))."\n";
	    
	    $score = "." unless (defined($score));
	    my $atts = "gene_id \"$blk\"; transcript_id \"$blk\";";
	    print F join("\t", ($chr, $track, "exon", $s, $e, $score, "+", ".",$atts))."\n";
	
	    $el++;
	}
    }
    close(F) if ($track);
    close(INFILE);
}


