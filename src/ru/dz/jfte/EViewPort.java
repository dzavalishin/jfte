package ru.dz.jfte;


public class EViewPort {


	public EView View;
	public int ReCenter;

	   EViewPort(EView V) { View = V; ReCenter = 0; }

	   void HandleEvent(TEvent Event) { }
	   void UpdateView() { }
	   void RepaintView() { }
	   void UpdateStatus() { }
	   void RepaintStatus() { }
	   void GetPos() { }
	   void StorePos() { }
	   void Resize(int Width, int Height) {}

}
