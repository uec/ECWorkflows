#!/usr/bin/perl

use strict;


while (<>)
{
   s/([^\x00-\x7f])/sprintf("&#%d;", ord($1))/ge;
   print;
}
