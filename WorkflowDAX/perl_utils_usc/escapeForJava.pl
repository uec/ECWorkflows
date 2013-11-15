#!/usr/bin/perl

use strict;


foreach my $line (<STDIN>)
{
    chomp $line;

    $line =~ s/\\/\\\\q/g;
    $line =~ s/\"/\\\"/g;
    my $a = chr(0xd);
    $line =~ s/$a//g;
    print "out += \"${line}\\n\";\n"; 
}
