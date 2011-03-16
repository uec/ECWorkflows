#!/usr/bin/perl

use strict;

    my $linecount_within = 0;
    my $seq_so_far = '';
    my $seq_line = '';
    while (my $line = <>)
    {
	if ($line =~ /^\@/)
	{
	    $linecount_within = 1;
	}
	else
	{
	    $linecount_within++;
	}
	
	$seq_so_far .= $line;
	
	if ($linecount_within == 2)
	{
	    $seq_line = $line;
	}
	elsif ($linecount_within == 4)
	{
	    if ($seq_line =~ /nnnnnnnnnnnnnnnnnnn/i) # N output
	    {
		# Nothing
	    }
	    else
	    {
		print $seq_so_far;
	    }
	    
	    $seq_so_far = '';
	    $seq_line = '';
	}
	
    }
