all: paper.pdf

clean:
	rm paper.pdf paper.log paper.blg paper.aux paper.bbl paper.toc

paper.bbl: paper.bib *.tex
	pdflatex paper.tex
	bibtex paper
	bibtex paper
	pdflatex paper.tex

paper.pdf: paper.bbl *.tex
	pdflatex paper.tex

