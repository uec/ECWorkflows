#!/usr/bin/perl

use strict;

my ($pattern) = @ARGV;
print STDERR "Searching for pattern $pattern\n";

my @lines_so_far = ();
while (my $line = <STDIN>)
{
    if ($line =~ /^>/)
    {
	# Check previous one
	printEntry(\@lines_so_far) if (@lines_so_far);

	# And start anew
	@lines_so_far = ();
    }
    
    push(@lines_so_far,$line);
}

# And check the last one
printEntry(\@lines_so_far) if (@lines_so_far);



sub printEntry
{
    my ($entry_lines) = @_;

    print join("",@$entry_lines)
	if (@{$entry_lines}[1] =~ /$pattern/);
}
