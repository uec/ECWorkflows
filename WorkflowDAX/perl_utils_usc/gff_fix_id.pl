#!/usr/bin/perl

use strict;
use File::Basename;

my @files = @ARGV;


foreach my $f (@files)
{

    my ($name, $path, $suf) = fileparse($f, qr/\.[^.]*/);
    my $outfn = join("", $path, $name, ".FIXED", $suf);
    print "($name)\t($path)\t($suf)\n";

    die "Couldn't read $f\n" unless (open(IN,$f));
    die "Couldn't write to $outfn\n" unless (open(OUT, ">$outfn"));
    my $count = 1;
    while (my $line = <IN>)
    {
	chomp $line;
	if ($line =~ /^\s*\#/ || $line =~ /^\s*$/ || $line =~ /track/)
	{
	    print OUT $line."\n";
	}
	elsif ($line =~ /random/i)
	{
	    # Remove chromosome random
	}
	else
	{
	    # Fix ID field.
	    my @flds = split(/\t/,$line);
	    my $id = @flds[8];
	    
	    my $new_id = $count++;
	    if ($id =~ /\w+\s+\"([^\"]+)\"\;/)
	    {
		$new_id .= "-".$1;
	    }
	    @flds[8] = "gene_id \"$new_id\"; transcript_id \"$new_id\";";


	    # Fix chr field if necessary
	    @flds[0] =~ s/chr23/chrX/g;
	    @flds[0] =~ s/chr24/chrY/g;
	    

	    # We also see if the source field needs correcting
	    if (@flds[1] =~ /^[\d\,]+$/)
	    {
		@flds[1] = "gff_fix_id";
	    }

	    # Also sometimes the 3rd field gets screwed up
	    @flds[2] = "exon";

	    # and print
	    print OUT join("\t",@flds)."\n";
	}
    }
    
    # Close input file
    close(IN);
    close(OUT);
}
