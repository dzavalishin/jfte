package ru.dz.jfte;


public abstract class EViewPort 
{
	protected EView View;
	protected int ReCenter;

	EViewPort(EView V) 
	{ 
		View = V; 
		ReCenter = 0; 
	}

	void HandleEvent(TEvent Event) { }
	abstract void UpdateView();// { }
	abstract void RepaintView();// { }
	abstract void UpdateStatus();// { }
	abstract void RepaintStatus();// { }
	abstract void GetPos();// { }
	abstract void StorePos();// { }
	void Resize(int Width, int Height) {}

}
