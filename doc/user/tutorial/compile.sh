#!/bin/bash

str=$1

if [ -z $str ]; then
    echo "No input file. Aborting."
    exit -1
fi

echo "Input file: $str"

# inkscape conversions of images
inkscape -f images/xenon-design.svg -E images/xenon-design.eps

# count the number of characters in the input filename
nChars=`expr ${#str} - 4`

# define ext as the last few characters of $str:
ext=${str:nChars}

# test if the input file has a .tex extension:
if [ $ext != ".tex" ]; then
    echo "Not a tex file. Aborting."
    exit -1
fi

# redefine str as only character 0 to nChars from original str
str=${str:0:nChars}


pdflatex -interaction=batchmode ${str}.tex 1> /dev/null
#bibtex ${str}.aux
makeindex ${str}.idx
pdflatex -interaction=batchmode ${str}.tex 1> /dev/null
pdflatex -interaction=batchmode ${str}.tex
echo

echo "Making a copy of '${str}.tex' with added line breaks for easier diff in git"
fmt -u ${str}.tex > ${str}.tex.fmt


grep -nE "\bTODO|\bFIXME" ${str}.tex

nTodos=`grep TODO ${str}.tex.fmt | wc -l`
if [ ${nTodos} -eq 1 ];
then
echo
echo "There is still "${nTodos}" TODO to attend to.";
fi

if [ ${nTodos} -gt 1 ];
then
echo
echo "There are still "${nTodos}" TODOs to attend to.";
fi



nFixmes=`grep FIXME ${str}.tex.fmt | wc -l`

if [ ${nFixmes} -eq 1 ];
then
echo "There is still "${nFixmes}" FIXME to attend to.";
echo
fi

if [ ${nFixmes} -gt 1 ];
then
echo "There are still "${nFixmes}" FIXMEs to attend to.";
echo
fi




